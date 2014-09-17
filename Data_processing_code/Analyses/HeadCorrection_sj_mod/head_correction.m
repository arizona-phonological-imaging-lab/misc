function head_correction(inputfolder)

% This program takes in a folder containing two neutral tongue contour
% files.  One must have 'C1_' (Contour 1) somewhere in its names.  The other 
% must have 'C2_' somewhere in its name.  The program will find the optimal
% transformation of C2 such that it aligns as well as it can to C1.  Then, it 
% will apply that transformation to C2 and to any other tongue contours in
% the folder.  It will create new .jpg.traced.txt files for the aligned
% contours.  It is important to note that the neutral contour labeled 
% as C2 should be the one which corresponds to all the other files to be
% aligned.  In other words, if you put two vowel contours from repetition 2
% of a word into the input folder, the neutral contour labeled C2 should be 
% the one taken from repetition 2.  If you don't do this, they will be 
% incorrectly transformed.  

% This program also produces a .txt file which lists the rotation and
% translation that optimally aligned C2 with C1.  

% This program requires the following matlab scripts:
% 1.  transform_tongue.m
% 2.  curve_avg_distance.m
% 3.  tongue_matcher2.m
% 4.  tongue_parabola.m


% Read in the files in the names of the files in the given folder.  Create
% a matrix of the file names. 
filelist = ls(inputfolder);



[rows,cols] = size(filelist);
ss = '';
i = 1;
for i=1:cols;
    ss = strcat(ss, filelist(1,i));
    i = i + 1;
end
contourfiles = regexp(ss, '.txt', 'split');
[row2,cols2] = size(contourfiles);
i = 1;
for i = 1:cols2
    contourfiles(1,i) = strcat(inputfolder, '/', contourfiles(1,i),'.txt');
    i = i + 1;
end
blankstr = strcat(inputfolder, '/', '.txt');
nonblankcols = find(strcmp(contourfiles(1,:), blankstr) == 0);
contourfiles = contourfiles(:,nonblankcols);


% So, contourfiles is a one row, multi-column matrix of pathnames to the
% tongue contours we're going to be looking at.  

% The tongue contour which will serve as the target of rotation and
% translation must have 'C1_' in its name.  The neutral tongue contour that 
% we're going to transform to be as close to C1 as possible must have 'C2_' 
% in its name. 

[row3,cols3] = size(contourfiles);

i = 1;
TongueContours = []; % A one row by multiple columns matrix of tongue contours to change.
for i = 1:cols3
    a = strfind(contourfiles(1,i), 'C1_'); % a and b are cells with index data.
    b = strfind(contourfiles(1,i), 'C2_');
    if a{:} ~= 0 % This checks the contents of the cell a.
        C1_pathname = contourfiles(1,i);
    elseif b{:} ~= 0
        C2_pathname = contourfiles(1,i);
    else
        TongueContours = [TongueContours,[contourfiles(1,i)]];
    end
    i = i + 1;
end


% Now that we have the filenames of C1, C2 and the other tongue contours,
% we read into C1 and C2.
C1_pathname = C1_pathname{:};
C2_pathname = C2_pathname{:};
NeutralTonguePaths = ({C1_pathname,C2_pathname});
NeutralTongueNames = ({'C1';'C2'});

i = 1;
for i=1:2
    data = load(char(NeutralTonguePaths(1,i)));
    data = data(:,2:3);
    data_subset = find(data(:,1) ~= -1);
    s1 = sprintf('%s = data(data_subset,:)',char(NeutralTongueNames(i)));
    eval(s1);  % Really cool construct that allows me to intelligently name variables!
    i = i + 1;
end

paraboladata = tongue_parabola(C1,C2);
C1_P1 = paraboladata.P1;
C2_P2 = paraboladata.P2;

% Plot C1 and C2 to give the user the chance to pick smart initial shift and
% rotation value.
plot(C1_P1(:,1),-C1_P1(:,2),'b','LineWidth',4);
    hold on;
plot(C2_P2(:,1),-C2_P2(:,2),'-g', 'LineWidth',4);
legend('Curve 1', 'Curve 2')
title('Neutral Tongue Curves')
    hold off;

% Ask user how much C2 needs to be shifted and rotated in order to make it
% align with C1.

%Edit 5-6: Sam Johnston
%this to allow for mass correction while looping through files
%auto = input('Do you want the program to make an initial guess as to how to\nbest align the curves or would you prefer to do so?\nPossible responses:  ''me'', ''pr'' (use quotes).  Response: ' )

%replaces the above with a default, do have the program automatically
%translat contour
auto = 'pr';

if auto == 'me'
    xs = input('How much does x need to be shifted by to move Curve 2 onto Curve 1?\nSample responses: 15, -20 (no quotes).  Response: ')
    ys = input('How much does y need to be shifted by to move Curve 2 onto Curve 1?\nSample responses: 15, -20 (no quotes).  Response: ')
    theta = input('How many degrees does Curve 2 need to be rotated in order to align\nit with Curve 1? Sample responses: 45, -30 (no quotes).\nNote that a positive rotation goes counterclockwise.  Response: ')
    % Change degrees into radians.
    theta = theta*pi/180;
else
    xs = paraboladata.xs;
    ys = paraboladata.ys;
    theta = paraboladata.theta;
end

% Find the best transformation to bring C2 into alignment with C1.    
parameters = tongue_matcher2(C1_P1,C2_P2,theta,xs,ys);
theta = parameters(1);
xs = parameters(2);
ys = parameters(3);

% Read in other tongue curves.
i = 1;
for i=1:length(TongueContours)

    data = load(char(TongueContours(1,i)));
    data = data(:,2:3);
    data_subset = find(data(:,1) ~= -1);
    s1 = sprintf('T%02d = data(data_subset,:)',i);
    eval(s1);  % Really cool construct that allows me to intelligently name variables!
    i = i + 1;
end

% Apply the best rotation and translation to C2.
rotmat = [cos(theta),-sin(theta);sin(theta),cos(theta)];
contour_temp = rotmat*C2';
[r,c] = size(contour_temp);
transmat = repmat([xs;ys],1,c);
new_C2 = (contour_temp + transmat)';
[r,c] = size(new_C2);
sidenums = zeros(r,1);
j = 1;
for j=1:r
    sidenums(j,1) = sidenums(j,1)+j;
    j = j + 1;
end
new_C2 = [sidenums,new_C2];


% Now, apply the best rotation and translation to the curves.
i = 1;
for i = 1:length(TongueContours)
    s2 = sprintf('contour_temp = rotmat*T%02d''',i);
    eval(s2);
    [r,c] = size(contour_temp);
    transmat = repmat([xs;ys],1,c);
    s3 = sprintf('new_T%02d = (contour_temp + transmat)''',i);
    eval(s3);
    s4 = sprintf('[r,c] = size(new_T%02d)',i);
    eval(s4);
    sidenums = zeros(r,1);
    j = 1;
    for j=1:r
        sidenums(j,1)=sidenums(j,1)+j;
        j = j + 1;
    end
    s5 = sprintf('new_T%02d = [sidenums,new_T%02d]',[i,i]);
    eval(s5);
    i = i + 1;
end

% Finally, print a file for each transformed contour.
basic_name = regexp(C2_pathname, '/', 'split');
new_pathname = strcat(inputfolder, '/', 'NEW_', basic_name(length(basic_name)));
fid = fopen(new_pathname{:}, 'w');
fprintf(fid, '%d\t%d\t%d\n', new_C2');
fclose(fid);

i = 1;
for i = 1:length(TongueContours)
%     disp(char(TongueContours(1,i)))
%     input('Press enter to continue');
    basic_name = regexp(char(TongueContours(i)), '/', 'split');
    new_pathname = strcat(inputfolder, '/', 'NEW_', basic_name(length(basic_name)));
%     input('Press enter to continue');
    fid = fopen(new_pathname{:}, 'w');
    s4= sprintf('fprintf(fid,''%%d\\t%%d\\t%%d\\n'',new_T%02d'')',i);
    eval(s4);
    fclose(fid);
%     disp(char(TongueContours(1,i)))
%     input('Press enter to continue');
end

% print out transformation details file.
transformation_pathname = strcat(inputfolder, '/', 'transformation_C2toC1.txt');
fid = fopen(transformation_pathname, 'w');
fprintf(fid, 'Transformation Details\n');
fprintf(fid, 'rotation in radians: %d\n', theta);
fprintf(fid, 'x shift: %d\n', xs);
fprintf(fid, 'y shift: %d', ys);
fclose(fid);







        




    